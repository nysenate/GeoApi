class BulkGeosController < ApplicationController
  # GET /bulk_geos
  # GET /bulk_geos.xml
  def index
    json = params[:json]

    geo = BulkGeo.new();
    
    render :json => geo.process(json)

  end
end
