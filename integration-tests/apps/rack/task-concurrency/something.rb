require 'torquebox-messaging'

class Something

  include TorqueBox::Messaging::Backgroundable
  always_background :foo

  def initialize
    @backchannel = TorqueBox::Messaging::Queue.new("/queues/backchannel")
  end

  def foo(index)
    @backchannel.publish "bg#{index}-sleep"
    sleep(1) 
    @backchannel.publish "bg#{index}-awake"
  end
end
