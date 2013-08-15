require 'torquebox-stomp'
require 'torquebox-web'

app = lambda { |env| 
  puts "Invoking app"
  session = env['rack.session']
  session[:food] ||= 'tacos'
  [200, { 'Content-Type' => 'text/html; charset=utf-8' }, "<div id='success' class='basic-rack #{ENV['GRIST']}'>it worked</div><div id='ruby-version'>#{RUBY_VERSION}</div>"] 
}

use TorqueBox::Session::ServletStore
run app
