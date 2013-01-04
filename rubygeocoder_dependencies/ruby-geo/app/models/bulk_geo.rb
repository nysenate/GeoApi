require 'rubygems'
require 'geocoder/us/database'
require 'json'

class BulkGeo 
  @@db = Geocoder::US::Database.new("/home/ubuntu/ruby/geo/ga5/db.db")
  def process (json)
    raise Argument, "no text provided" unless json and !json.empty?
    
    parsed_list = JSON.parse(json)
    
    res = ""   
 
    parsed_list.each {|addr|
      hash = Hash.new
      hash[:street] = addr["street"] unless !addr["street"] or addr["street"].empty?
      hash[:city] = addr["city"] unless !addr["city"] or addr["city"].empty?
      hash[:state] = addr["state"] unless !addr["state"] or addr["state"].empty?
      hash[:postal_code] = addr["zip5"] unless !addr["zip5"] or addr["zip5"].empty?
      
      geo_list = @@db.geocode(hash)
      if !geo_list or geo_list.empty?
        res += "{},"
      else
        res += geo_list[0].to_json + ","
      end
      #res = res + @@db.geocode(hash)[0] + ","
    }
    @@db.close
    res.gsub(/,$/,'') 
  end
end
