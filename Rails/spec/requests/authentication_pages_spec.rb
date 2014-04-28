require 'spec_helper'

describe "Authentication" do
  before do
    @user = User.create(name: "Example User 1", email: "user1@example.com",
                     password: "foobar", password_confirmation: "foobar")
    visit signin_path
  end

  subject { page }


  it "has no user" do
    # should have_title("Sleepstream - Sign in")
    should have_content("Sign up now")
    should have_content("Email")
    should have_content("Password")
    should have_content("Sign in")
  end

  describe "sign in with wront details" do
    before do
      click_button('Sign in')
    end
    it { should have_content('Invalid')}
  end

  describe "sign in with good details" do
    before do
      fill_in "Email",    with: @user.email
      fill_in "Password", with: @user.password
      click_button('Sign in')
    end
    it { should have_content(@user.name)}
    it { @user.should_not be_admin }
  end
end