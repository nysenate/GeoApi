require 'rubygems'
class GeoCodesController < ApplicationController
  # GET /geo_codes
  # GET /geo_codes.xml
  def index
   addr = params[:address]
   
   geo = GeoCode.new()
   if(addr.nil? || addr == "")
     render :json => json = geo.process({:number => params[:number], :street => params[:street], :city => params[:city], :state => params[:state], :postal_code => params[:zip]}) 
   else
     render :text => geo.process(addr) 
   end
  end
end
