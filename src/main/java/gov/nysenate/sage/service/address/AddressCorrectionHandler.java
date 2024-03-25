package gov.nysenate.sage.service.address;

import com.google.common.collect.Range;
import gov.nysenate.sage.scripts.streetfinder.model.StreetfileAddressRange;
import gov.nysenate.sage.scripts.streetfinder.scripts.utils.Addr1WithZip;
import gov.nysenate.sage.scripts.streetfinder.scripts.utils.AddressValidationResult;
import gov.nysenate.sage.util.Pair;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Contains various methods to held convert raw StreetFileAddressRanges into USPS corrected and validated ones.
 */
public class AddressCorrectionHandler {
    /**
     * Returns a list of StreetFileAddressRange with validated and corrected address data.
     * One base StreetFileAddressRange may turn into two, if the streets or zips of the
     * low and high building don't match.
     * @throws IOException if there was a problem accessing the API.
     */
    public List<StreetfileAddressRange> getCorrectedAddressRanges(StreetfileAddressRange sfa) throws IOException {
        var originalLow = new Addr1WithZip(sfa, true);
        AddressValidationResult correctedLow = correctAddress(originalLow);
        if (!correctedLow.isValid()) {
            return List.of();
        }
        AddressValidationResult correctedHigh = correctedLow;
        // We should only run the high address if the range isn't a single address.
        if (!sfa.getBuildingRange().isSingleton()) {
            var originalHigh = new Addr1WithZip(sfa, false);
            correctedHigh = correctAddress(originalHigh);
            if (!correctedHigh.isValid()) {
                return List.of();
            }
            if (!correctedLow.addr().ignoreBuildingEquals(correctedHigh.addr())) {
                Pair<Range<Integer>> addrRanges = getValidRanges(correctedLow, correctedHigh);
                if (addrRanges == null) {
                    return List.of();
                }
                var sfa1 = new StreetfileAddressRange();
                var low1 = new Addr1WithZip(addrRanges.first().lowerEndpoint(), correctedLow.addr());
                setValues(sfa1, low1, String.valueOf(addrRanges.first().upperEndpoint()));

                var sfa2 = new StreetfileAddressRange();
                var low2 = new Addr1WithZip(addrRanges.second().lowerEndpoint(), correctedHigh.addr());
                setValues(sfa2, low2, String.valueOf(addrRanges.second().upperEndpoint()));
                return List.of(sfa1, sfa2);
            }
        }
        setValues(sfa, correctedLow.addr(), correctedHigh.addr().building());
        return List.of(sfa);
    }

    /**
     * Corrects the address values in a StreetFileAddressRange with USPS validated data.
     */
    private static void setValues(StreetfileAddressRange sfa, Addr1WithZip lowAddr, String highBldg) {
        sfa.setBuilding(true, lowAddr.building());
        sfa.setBuilding(false, highBldg);
        sfa.setStreet(lowAddr.street());
        sfa.setZip5(String.valueOf(lowAddr.zip()));
    }

    /**
     * Called if the low and high addresses have different zipcodes or streets.
     * We attempt to correct a Range where the street and zip match low or high.
     * @throws IOException if there was a problem accessing the API.
     */
    // TODO: range is wrongfully excluding top of low range
    private Pair<Range<Integer>> getValidRanges(AddressValidationResult low, AddressValidationResult high) throws IOException {
        // TODO check for ALL parity
        Set<Addr1WithZip> validAddresses = new HashSet<>(Set.of(low.addr(), high.addr()));
        int currBldgNum = low.withinRange().upperEndpoint();
        AddressValidationResult currAddrData = low;
        while (!high.addr().ignoreBuildingEquals(currAddrData.addr()) &&
                currBldgNum < high.withinRange().lowerEndpoint()) {
            var nextAddr = new Addr1WithZip(currBldgNum + 2, high.addr());
            currAddrData = correctAddress(nextAddr);
            if (currAddrData.isValid()) {
                validAddresses.add(currAddrData.addr());
                currBldgNum = currAddrData.withinRange().upperEndpoint();
            }
            // Addresses need not be continuous: we should keep trying.
            else {
                currBldgNum += 2;
            }
        }
        try {
            return new Pair<>(getRange(low.addr(), validAddresses), getRange(high.addr(), validAddresses));
        }
        // We currently can't parse non-numeric ranges
        catch (NumberFormatException ex) {
            return null;
        }
    }

    // TODO
    private AddressValidationResult correctAddress(Addr1WithZip input) {
        return null;
    }

    /**
     * Filters a set of Addr1WithZips to those that match the street and zip of a base Addr1WithZip,
     * and converts to a range.
     * @param base to compare street and zip.
     * @param addr1WithZips to filter.
     * @return a Range covering all Addr1WithZips that meet the filter.
     */
    private static Range<Integer> getRange(Addr1WithZip base, Set<Addr1WithZip> addr1WithZips) {
        return Range.encloseAll(addr1WithZips.stream()
                .filter(addr -> addr.ignoreBuildingEquals(base))
                .map(addr -> Integer.parseInt(addr.building()))
                .collect(Collectors.toList()));
    }
}
