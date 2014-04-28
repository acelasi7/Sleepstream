class RelationshipsController < ApplicationController
  before_action :signed_in_user

  def create
    if params[:user][:email].empty? || User.find_by(email: params[:user][:email]).nil? || !(User.find_by(email: params[:user][:email]).supervisor?)
      flash[:error] = "Email address doesn't match"
      redirect_to supervisor_path
    else
      @user = User.find_by(email: params[:user][:email])
      if current_user.supervise!(@user)
        respond_to do |format|
          format.html { redirect_to supervisor_path }
          format.js
        end
      else
        redirect_to supervisor_path
      end
    end
  end

  def destroy

    Relationship.find(params[:id]).destroy
    respond_to do |format|
      format.html { redirect_to current_user }
      format.js
    end
  end
end