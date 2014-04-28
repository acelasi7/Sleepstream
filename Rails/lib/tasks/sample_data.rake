namespace :db do
  desc "Fill database with sample data"
  task populate: :environment do
    make_users
    make_relationships
  end
end

def make_users
  admin = User.create!(name:     "Example User",
                       email:    "example@railstutorial.org",
                       password: "foobar",
                       password_confirmation: "foobar",
                       admin: true)
  99.times do |n|
    name  = Faker::Name.name
    email = "example-#{n+1}@railstutorial.org"
    password  = "password"
    User.create!(name:     name,
                 email:    email,
                 password: password,
                 password_confirmation: password)
  end
  users = User.all(limit: 6)
  50.times do
      content = Faker::Lorem.sentence(5)
      users.each { |user| user.microposts.create!(content: content) }
    end
end

def make_relationships
  users = User.all
  user  = users.first
  supervised_users = users[2..50]
  supervisors      = users[3..40]
  supervised_users.each { |supervised| user.follow!(supervised) }
  supervisors.each      { |supervisor| supervisor.follow!(user) }
end