require 'rubygems'
#require 'bleak_house'


ENV['GEM_PATH'] = '/usr/lib/ruby/gems/1.8'
ENV['GEM_HOME'] = '$GEM_PATH'
ENV['RUBYOPT'] = 'rubygems'
Gem.clear_paths

# Load the rails application
require File.expand_path('../application', __FILE__)

# Initialize the rails application
Webapp::Application.initialize!

#MemoryProfiler.start
#ENV['RAILS_ENV'] = 'production'
#ENV['BLEAK_HOUSE'] = '1'


#require 'bleak_house' if ENV['BLEAK_HOUSE']
