class CreateSleeps < ActiveRecord::Migration
  def change
    create_table :sleeps do |t|
      t.integer :user_id
      t.string :content

      t.timestamps
    end
    add_index :sleeps, [:user_id, :created_at]
  end
end
