class Api::V1::SessionsController < ApiController
  skip_before_filter :authenticate, :only => :create

  respond_to :json

  def new
    end

  def index
    render :json => {}
  end

  def create
    user = User.find_by_email(params[:email].to_s.downcase)

    if user && user.authenticate(params[:password])
      # Sign the user in and redirect to the user's show page.
      access_token = User.new_remember_token
      user.update_attribute(:access_token, User.encrypt(access_token))
      self.current_user = user
      respond_to do |f| 
        f.json {render json: {status: "success", access_token: access_token, id: user.id}}
        #the 3rd part takes this token and uses it for further logins
      end
    else
      # Create an error message and re-render the signin form.
      respond_to do |f| 
        f.json {render json: {status: "fail"}}
      end
    end
  end

    def destroy
      sign_out
    end

    
end