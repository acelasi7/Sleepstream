class Api::V1::SleepsController < ApiController
	
	respond_to :json

	def index
		respond_with ala: "da"
	end

	def create
		@sleep = @current_user.sleeps.build(content: params[:content])
		if @sleep.save
			#response_with json: {status: "success"}
			respond_to do |f| 
        		f.json {render json: {status: "success", sleep_id: @sleep.id}}
      		end
		else
			#response_with json: {status: "fail"}
			respond_to do |f| 
        		f.json {render json: {status: "fail"}}
      		end
		end
	end

	def update
		@sleep = @current_user.sleeps.find_by_id(params[:id])
		if @sleep && @sleep.update_attribute(:content, @sleep.content + ',' + params[:content])
			respond_to do |f| 
        		f.json {render json: {status: "success", sleep_id: @sleep.id}}
      		end
      	else
      		respond_to do |f| 
        		f.json {render json: {status: "fail"}}
      		end
      	end
	end

end