class ApiController < ActionController::Base
	include SessionsHelper

	before_filter :authenticate
	respond_to :json


	def sign_out user
		user = User.find_by(email: params[:email].downcase)
		user.update_attribute(:access_token, User.encrypt(User.new_remember_token))
	end

# private

# 	def restrict_access
#         # @current_user = User.find_by_remember_token(User.encrypt(params[:access_token]))
#         # head :unauthorized unless @current_user

#         authenticate_or_request_with_http_token do |access_token, options|
# 					@current_user = User.find_by_remember_token(User.encrypt(access_token))
# 		end
#     end

#     # def render_unauthorized
#     #   self.headers['WWW-Authenticate'] = 'Token realm="Application"'
#     #   render json: 'Bad credentials', status: 401
#     # end

protected
    def authenticate
      authenticate_token || render_unauthorized
    end

    def authenticate_token
      authenticate_with_http_token do |token, options|
        @current_user = User.find_by_access_token(User.encrypt(token))
      end
    end

    def render_unauthorized
      self.headers['WWW-Authenticate'] = 'Token realm="Application"'
      render json: {status: "fail"}, status: 401
    end

end