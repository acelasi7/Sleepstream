require 'spec_helper'

describe SupervisorUser do
  before do
    @user1 = User.new(name: "Example User 1", email: "user1@example.com",
                     password: "foobar", password_confirmation: "foobar")
    @user2 = User.new(name: "Example User 2", email: "user2@example.com",
                     password: "foobar", password_confirmation: "foobar")
    @supervisor_user = @user1.supervisor_user.build(supervisor_id: @user2.id)
  end
  subject { @supervisor_user }

  describe "follower methods" do
    it { should respond_to(:supervisor) }
    it { should respond_to(:supervised) }
    its(:supervisor) { should eq supervisor }
    its(:supervised) { should eq supervised }
  end

  describe "when supervised id is not present" do
    before { supervisor_user.supervised_id = nil }
    it { should_not be_valid }
  end

  describe "when supervisor id is not present" do
    before { supervisor_user.supervisor_id = nil }
    it { should_not be_valid }
  end
end
