package el.team_application.ActivityViews.Activities;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.Parse;

import java.util.LinkedList;
import java.util.List;

import el.team_application.Listeners.Teams.GetMyTeamsListener;
import el.team_application.Listeners.User.GetSessionCallback;
import el.team_application.Models.Entities.Team;
import el.team_application.Models.Entities.TeamMember;
import el.team_application.Models.Entities.User;
import el.team_application.Models.Model;
import el.team_application.R;

public class MyTeamsActivity extends ActionBarActivity {
    ListView            listView;
    MyTeamsAdapter      adapter;
    User                loggedInUser;
    List<Team>          teams = new LinkedList<Team>();
    static final int    NEW_TEAM_REQUEST = 1;
    ProgressBar pb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_teams);

        loggedInUser = Model.getInstance().getLoggedInUser();

        listView = (ListView) findViewById(R.id.myteam_list);
        pb = (ProgressBar)findViewById(R.id.loadingSpinner);
        adapter  = new MyTeamsAdapter();
        listView.setAdapter(adapter);

        final TextView emptyText = (TextView) findViewById(R.id.myteams_empty_message);
        listView.setEmptyView(emptyText);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(getApplicationContext(), "item click " + position, Toast.LENGTH_LONG).show();
                Intent intent = new Intent(getApplicationContext(), TeamActivity.class);
                intent.putExtra("id", teams.get(position).getId());
                startActivity(intent);
            }
        });

        requestMyTeams();
    }

    private void requestMyTeams() {
        Model.getInstance().getMyTeams(loggedInUser.getId(), new GetMyTeamsListener() {
            @Override
            public void onResult(List<Team> myTeams, Exception e) {
                teams = myTeams;
                adapter.notifyDataSetChanged();
                pb.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_my_teams, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        Intent intent;
        //noinspection SimplifiableIfStatement
        switch (id){
            case R.id.menu_my_teams_create_team:
                Toast.makeText(getApplicationContext(), "Create new Team", Toast.LENGTH_LONG).show();
                intent = new Intent(getApplicationContext(),CreateTeamActivity.class);
                startActivityForResult(intent, NEW_TEAM_REQUEST);
                return true;
            case R.id.menu_my_teams_action_logoff:
                Toast.makeText(getApplicationContext(), "Logoff", Toast.LENGTH_LONG).show();
                Model.getInstance().logout();
                intent = new Intent(getApplicationContext(),WelcomeActivity.class);
                startActivity(intent);
                finish();
                return true;
            case R.id.menu_my_teams_action_exit:
                Toast.makeText(getApplicationContext(), "Exit", Toast.LENGTH_LONG).show();
                finish();
                System.exit(0);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
//        requestMyTeams();
        // Check which request we're responding to
        switch(requestCode){
            case NEW_TEAM_REQUEST: {
                // Make sure the request was successful
                if (resultCode == RESULT_OK) {
                    requestMyTeams();
                }
            }
        }
    }

    private void newTeam() {
        Toast.makeText(getApplicationContext(), "Create new Team", Toast.LENGTH_LONG).show();
        Intent intent = new Intent(getApplicationContext(),CreateTeamActivity.class);
        startActivityForResult(intent, NEW_TEAM_REQUEST);
    }

    class MyTeamsAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return teams.size();
        }

        @Override
        public Object getItem(int position) {
            return teams.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if(convertView == null){
                LayoutInflater inflater = getLayoutInflater();
                convertView = inflater.inflate(R.layout.myteams_list_item,null);
            }

            TextView teamName = (TextView) convertView.findViewById(R.id.myteams_row_name_tv);
            TextView managerName = (TextView) convertView.findViewById(R.id.myteams_row_manager_tv);
            convertView.setTag(position);

            TeamMember manager = null;
            List<TeamMember> members = teams.get(position).getMemberList();
            for (TeamMember member : members){
                if(member.getRole().equals(TeamMember.Role.MANAGER)){
                    manager = member;
                    break;
                }
            }

            String name = teams.get(position).getName();
            teamName.setText(name);
            managerName.setText(manager.getJobTitle());

            return convertView;
        }
    }

}
