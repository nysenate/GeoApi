package gov.nysenate.sage.service.streetfile;

import gov.nysenate.sage.dao.provider.usps.USPSAMSDao;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.result.AddressResult;
import gov.nysenate.sage.scripts.streetfinder.model.AddressWithoutNum;
import gov.nysenate.sage.scripts.streetfinder.scripts.utils.DistrictingData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public final class StreetfileAddressCorrectionService {
    private static final Logger logger = LoggerFactory.getLogger(StreetfileAddressCorrectionService.class);
    private static final Map<String, Integer> rankMap = Map.of("Address Component Changed", 1, "Status: DEFAULT_MATCH", 2);
    private static final int VALIDATION_BATCH_SIZE = 4000;
    private final USPSAMSDao amsDao;

    @Autowired
    public StreetfileAddressCorrectionService(USPSAMSDao amsDao) {
        this.amsDao = amsDao;
    }

    public Map<AddressWithoutNum, AddressWithoutNum> getCorrections(DistrictingData data) {
        Map<AddressWithoutNum, Queue<Integer>> toCorrectMap = data.rowToNumMap().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        return getCorrectionsHelper(toCorrectMap, 0);
    }

    private Map<AddressWithoutNum, AddressWithoutNum> getCorrectionsHelper(Map<AddressWithoutNum, Queue<Integer>> toCorrectMap, int minRank) {
        final var correctionMap = new HashMap<AddressWithoutNum, AddressWithoutNum>(toCorrectMap.size());
        var nextToCorrectMap = new HashMap<AddressWithoutNum, Queue<Integer>>();

        while (!toCorrectMap.isEmpty()) {
            List<AddressWithoutNum> awns = toCorrectMap.keySet().stream().limit(VALIDATION_BATCH_SIZE).toList();
            List<Integer> numsToValidate = awns.stream().map(awn -> toCorrectMap.get(awn).poll()).toList();
            List<Address> toValidate = IntStream.range(0, awns.size())
                    .mapToObj(index -> getAddress(numsToValidate.get(index), awns.get(index))).toList();
            List<AddressResult> validationResults = amsDao.getValidatedAddressResults(toValidate);

            for (int addrIndex = 0; addrIndex < validationResults.size(); addrIndex++) {
                AddressResult result = validationResults.get(addrIndex);
                AddressWithoutNum uncorrectedAwn = awns.get(addrIndex);
                if (!result.isValidated()) {
                    continue;
                }
                // We want to first try a correction without changing components.
                // This is especially relevant for NYC streetfiles, which may have ranges with currently non-existent addresses.
                // AMS will attempt to correct such addresses to a different, existing address.
                if (getRank(result.getMessages()) > minRank) {
                    nextToCorrectMap.computeIfAbsent(uncorrectedAwn, k -> new LinkedList<>())
                            .add(numsToValidate.get(addrIndex));
                    continue;
                }
                final var currCorrectedAwn = AddressWithoutNum.fromAddress(result.getAddress());
                AddressWithoutNum mapCorrectedAwn = correctionMap.get(uncorrectedAwn);
                if (mapCorrectedAwn == null) {
                    correctionMap.put(uncorrectedAwn, currCorrectedAwn);
                    toCorrectMap.remove(uncorrectedAwn);
                }
                else if (!currCorrectedAwn.equals(mapCorrectedAwn)) {
                    logger.error("Conflict between corrections: " + mapCorrectedAwn + " and " + currCorrectedAwn);
                }
            }
            // Removes AddressWithoutNums without any building numbers remaining to test.
            toCorrectMap.entrySet().stream()
                    .filter(entry -> entry.getValue().isEmpty())
                    .map(Map.Entry::getKey).toList().forEach(toCorrectMap::remove);
        }
        // Ensures initial corrections aren't overwritten.
        correctionMap.keySet().forEach(nextToCorrectMap::remove);
        // A second attempt at addresses who had a component changed.
        if (!nextToCorrectMap.isEmpty()) {
            logger.info("Corrected {}! Attempting to correct {} more...", correctionMap.size(), nextToCorrectMap.size());
            correctionMap.putAll(getCorrectionsHelper(nextToCorrectMap, minRank + 1));
        }
        return correctionMap;
    }

    private static int getRank(List<String> messages) {
        int rank = 0;
        for (var rankMapEntry : rankMap.entrySet()) {
            if (messages.stream().anyMatch(message -> message.startsWith(rankMapEntry.getKey()))) {
                rank += rankMapEntry.getValue();
            }
        }
        return rank;
    }

    private static Address getAddress(int num, AddressWithoutNum awn) {
        var addr = new Address(num + " " + awn.street());
        addr.setPostalCity(awn.postalCity());
        addr.setZip5(String.valueOf(awn.zip5()));
        addr.setState("NY");
        return addr;
    }
}
