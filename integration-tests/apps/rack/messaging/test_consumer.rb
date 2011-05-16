require 'torquebox-messaging'

class TestConsumer < TorqueBox::Messaging::MessageProcessor
  
  def on_message(body)
    puts "on_message: #{body}"
    TorqueBox::Messaging::Queue.new('/queues/results').publish( "result=#{body}" )
  end
  
end
