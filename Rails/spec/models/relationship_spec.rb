require 'spec_helper'

describe Relationship do
  #pending "add some examples to (or delete) #{__FILE__}"
  before do
    @followed = User.new(name: "Example User 1", email: "user1@example.com",
                     password: "foobar", password_confirmation: "foobar")
    @follower = User.new(name: "Example User 2", email: "user2@example.com",
                     password: "foobar", password_confirmation: "foobar", supervisor: "true")
    @follower_user = @followed.relationships.build(follower_id: @follower.id)
  end
  subject { @follower_user }

  describe "follower methods" do
    it { should respond_to(:follower) }
    it { should respond_to(:followed) }
    its(:follower_id) { should eq @follower.id }
    its(:followed_id) { should eq @followed.id }
  end

  describe "when supervised id is not present" do
    before { @follower_user.followed_id = nil }
    it { should_not be_valid }
  end

  describe "when supervisor id is not present" do
    before { @follower_user.follower_id = nil }
    it { should_not be_valid }
  end
end
