require 'spec_helper'

describe Sleep do
  let(:user) { FactoryGirl.create(:user) }
  before do
  	# @sleep = Sleep.new(content: "1,2,2,2,1,2,1" , user_id: user.id)
  	@sleep = user.sleeps.build(content: "1,2,2,1,2,1,2,")
  end

  subject { @sleep }

  it { should respond_to(:content) }
  it { should respond_to(:user_id) }
  it { should respond_to(:user)}
  its(:user) { should eq user }

  it { should be_valid }

  describe "when user_id is not present" do
  	before { @sleep.user_id =nil }
  	it { should_not be_valid }
  end
end
