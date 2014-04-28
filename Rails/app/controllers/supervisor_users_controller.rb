class SupervisorUsersController < ApplicationController
	before_action :signed_in_user

	def new
		@user = User.find(params[:user_id])
		@supervisor_user = @user.supervisor_users.build
		respond_with(@chapter)
	end

  	def create
  		@supervisor_user = current_user.supervisor_user.build(
  							supervisor_id: User.find(email: supervisor_user_params[:email]))
  		if @supervisor_user.save
  			flash[:success] = "Supervisor saved!"
  			redirect_to root_url
  		else
  			render 'static_page/home'
  		end
  	end

	def destroy
		@user = Relationship.find(params[:id]).supervised
    	current_user.unsupervise!(@user)
    	redirect_to supervisor_path
	end

	def index
		@user = User.find(params[:user_id])
		@supervisors = @user.supervisors
	end

	private

		def supervisor_params
			params.require(:supervisor_user).permit(:email)
		end
end
