FactoryGirl.define do
  factory :user do
    name     "Tudor Max"
    email    "blabla2@example.com"
    password "foobar"
    password_confirmation "foobar"
  end

  factory :sleep do
  	content "1,2,2,2,1,2,1,1,1,"
  	user
  end
  
end