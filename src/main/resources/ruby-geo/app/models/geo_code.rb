require 'rubygems'
require 'geocoder/us/database'
require 'json'

class GeoCode 
   @@db = Geocoder::US::Database.new("/home/ubuntu/ruby/geo/ga5/db.db", {:cache_size => 20000})
   attr_accessor :addr
   attr_accessor :number
   attr_accessor :street
   attr_accessor :city
   attr_accessor :state
   attr_accessor :zip

   def process (text)
      raise Argumemnt, "no text provided" unless text and !text.empty?
      if text.class == Hash
         #broken up
         do_hash text
      else
         #address
         do_text text
      end
   end
   
   def do_hash (hash)
      @number = hash[:number]
      @street = hash[:street]
      @city = hash[:city]
      @state = hash[:state]
      @zip = hash[:postal_code]
      
      hash.delete(:number) if number.nil?
      
      do_write @@db.geocode(hash).to_json unless (!number && !street) or !street or !city or !state or !zip
   end
   
   def do_text (text)
      do_write @@db.geocode(text).to_json unless !text 
   end

   def do_write (text)
      @@db.close
      text 
   end
end
