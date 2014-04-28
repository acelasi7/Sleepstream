class SleepsController < ApplicationController
	#before_action :signed_in_user
	
	def create
		@sleep = current_user.sleeps.build(sleep_params)
		if @sleep.save
			flash[:success] = "Micropost created!"
			redirect_to root_url
		else
			render 'static_pages/home'
		end
	end

	def destroy
		@sleep = Sleep.find(params[:id])
		@user = User.find(@sleep.user_id)
		if Sleep.find(params[:id]) && Sleep.find(params[:id]).destroy
			respond_to do |format|
      			format.html { @user}
      			format.js
    		end
    	else
    		respond_to do |format|
      			format.html { render json: params[:id] }
      			format.js
    		end
    	end
	end

	private

		def sleep_params
			params.require(:sleep).permit(:content)
		end
end
