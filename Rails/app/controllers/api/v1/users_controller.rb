class Api::V1::UsersController < ApiController
	class User < ::User
		def as_json(options={})

			super.merge(sleeps: sleeps)
		end
	end

respond_to :json

	def index
		@users = User.all
		respond_with @users
	end

	def show
		@user = User.find_by_id(params[:id])
		@sleeps = @user.sleeps
		respond_with @user
	end

end