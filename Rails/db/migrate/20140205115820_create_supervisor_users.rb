class CreateSupervisorUsers < ActiveRecord::Migration
  def change
    create_table :supervisor_users do |t|
      t.integer :supervisor_id
      t.integer :supervised_id

      t.timestamps
    end
    add_index :supervisor_users, :supervisor_id
    add_index :supervisor_users, :supervised_id
    add_index :supervisor_users, [:supervisor_id, :supervised_id], unique: true
  end
end
