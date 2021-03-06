require 'spec_helper'

describe "Micropost pages" do

  subject { page }

  let(:user) { FactoryGirl.create(:user) }
  before { sign_in user }

  describe "sleep creation" do
    before { visit root_path }

    describe "with invalid information" do

      it "should not create a sleep" do
        expect { click_button "Post" }.not_to change(Sleep, :count)
      end

      describe "error messages" do
        before { click_button "Post" }
        it { should have_content('error') }
      end
    end

    describe "with valid information" do

      before { fill_in 'sleep_content', with: "1,2,3," }
      it "should create a micropost" do
        expect { click_button "Post" }.to change(Sleep, :count).by(1)
      end
    end
  end
end