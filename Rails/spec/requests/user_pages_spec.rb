require 'spec_helper'

describe "User pages" do
  before do
    @user = User.create(name: "Example User 1", email: "user1@example.com",
                     password: "foobar", password_confirmation: "foobar")
    # sign_in @user
  end

  subject { page }

  describe "profile page" do
    it "contains name" do
      # should have_title("Sleepstream")
    end
  end

  describe "edit page" do
  end

  describe "supervisor page" do
  end

  describe "sign in with wront details" do
    before do
      visit signin_path
      click_button('Sign in')
    end
    it { should have_content('Invalid')}
  end

  describe "sign in with good details" do
    before do
      visit signin_path
      fill_in "Email",    with: @user.email
      fill_in "Password", with: @user.password
      click_button('Sign in')
    end
    it { should have_content(@user.name)}
  end

end