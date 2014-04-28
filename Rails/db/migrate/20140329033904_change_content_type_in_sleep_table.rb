class ChangeContentTypeInSleepTable < ActiveRecord::Migration
  def change
  	change_column :sleeps, :content, :text, :limit => nil
  end
end
