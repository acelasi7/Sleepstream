class UsersController < ApplicationController
    before_action :signed_in_user,
            only: [:index, :edit, :update, :destroy, :following, :followers]
    before_action :correct_user,   only: [:edit, :update]
    before_action :admin_user,     only: :destroy

    def following
        @title = "Supervising"
        @user = User.find(params[:id])
        @users = @user.followed_users.paginate(page: params[:page])
        render 'show_follow'
    end

    def followers
        @title = "Supervisors"
        @user = User.find(params[:id])
        @users = @user.followers.paginate(page: params[:page])
        render 'show_follow'
    end

    def index
        if current_user.admin?
            @users = User.all.order('created_at')
        else
            flash[:error] = "You don't have permission"
            redirect_to root_url
        end
    end

	def show
        @user = User.find(params[:id])
        @sleeps = @user.sleeps.order('created_at desc')
        if @user.sleeps.any?
            @sleep_data = @user.sleeps.last.content.split(",").map(&:to_i)
        end
    end
  	
  	def new
  		@user = User.new
  	end

  	def create
        @user = User.new(user_params)
        if @user.save
            sign_in @user
            flash[:success] = "Welcome to the SleepStream!"
            redirect_to @user
        else
            render 'new'
        end
    end

    def edit
    end

    def update
        if @user.update_attributes(user_params)
            flash[:success] = "Profile updated"
            redirect_to @user
        else
            render 'edit'
        end
    end

    def destroy
        unless User.find(params[:id]).admin?
            User.find(params[:id]).destroy
            flash[:success] = "User deleted."
        end
        redirect_to users_url
    end

    def show_supervisor
        @user = current_user  
    end

    def show_supervised_users
        @user = current_user
    end

    def supervisor
        @user = current_user
        @supervisor_user = @user.supervisor_users.build
        if User.find(params[:email].supervise!(current_user))
            flash[:success] = "Supervisor added."
            redirect_to supervisor_path
        else
            flash[:error] = "Something went wrong."
            redirect_to supervisor_path
        end
    end

private

    def user_params
        params.require(:user).permit(:name, :email, :password,
                                   :password_confirmation, :supervisor)
    end

    # Before filters

    def correct_user
        @user = User.find(params[:id])
        redirect_to(root_url) unless current_user?(@user)
    end

    def admin_user
        redirect_to(root_url) unless current_user.admin?
    end
    
end
