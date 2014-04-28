class SupervisorUser < ActiveRecord::Base
	belongs_to :supervisor, class_name: "User"
	belongs_to :supervised, class_name: "User"
	validates :supervisor_id, presence: true
	validates :supervised_id, presence: true
end
