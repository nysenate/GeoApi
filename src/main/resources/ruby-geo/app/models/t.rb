require 'rubygems'
require 'geocoder/us/database'
require 'fastercsv'


@@db = Geocoder::US::Database.new("/home/ubuntu/ruby/geo/ga1/db1.db")

begin
	if !ARGV[0].nil?			
		p @@db.geocode(ARGV[0])
	else
		p @@db.geocode({:street => "207 Maiden Lane", :city => "Port Jefferson", :state=>"NY", :postal_code => "11777"},true)
	end
	@@db.close
rescue Exception => e
	puts e.message
end
