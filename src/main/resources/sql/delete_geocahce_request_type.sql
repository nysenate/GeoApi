
delete from log.geocoderesult where id in (select geocoderequestid
                                            from log.geocoderequest, log.apirequest, log.geocoderesult
                                            where log.apirequest.requesttypeid =15 and
                                                  log.geocoderequest.apirequestid = log.apirequest.id
                                                  and log.geocoderesult.geocoderequestid = log.geocoderequest.id);


delete from log.geocoderequest where id in (select log.geocoderequest.id
                                            from log.geocoderequest, log.apirequest
                                            where log.apirequest.requesttypeid =15 and
                                                  log.geocoderequest.apirequestid = log.apirequest.id);

delete from log.apirequest where requesttypeid = 15;
delete from log.requesttypes where name = 'geocache';