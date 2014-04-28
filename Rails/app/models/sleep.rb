class Sleep < ActiveRecord::Base
	belongs_to :user

	#default_scope -> { order('created_at DESC')}
	validates :content, presence: true
	validates :user_id, presence: true
	VALID_SLEEP_REGEX = /\A[\d\,]*\z/
 	validates :content, format: { with: VALID_SLEEP_REGEX }
end
