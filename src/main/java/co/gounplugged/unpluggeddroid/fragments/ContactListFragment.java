package co.gounplugged.unpluggeddroid.fragments;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.AbsListView;
import android.widget.AdapterView;

import java.util.List;

import co.gounplugged.unpluggeddroid.R;
import co.gounplugged.unpluggeddroid.activities.BaseActivity;
import co.gounplugged.unpluggeddroid.activities.ChatActivity;
import co.gounplugged.unpluggeddroid.adapters.ContactAdapter;
import co.gounplugged.unpluggeddroid.events.ConversationEvent;
import co.gounplugged.unpluggeddroid.exceptions.InvalidConversationException;
import co.gounplugged.unpluggeddroid.exceptions.NotFoundInDatabaseException;
import co.gounplugged.unpluggeddroid.models.Contact;
import co.gounplugged.unpluggeddroid.models.Conversation;
import co.gounplugged.unpluggeddroid.utils.ContactUtil;
import co.gounplugged.unpluggeddroid.utils.ConversationUtil;
import de.greenrobot.event.EventBus;

public class ContactListFragment  extends ListFragment implements AdapterView.OnItemClickListener{
    private final static String TAG = "ContactListFragment";

    private ContactAdapter mContactAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Get super view and add custom layout to it to make sure setListShown and other helpers are accessible
        View v = super.onCreateView(inflater, container, savedInstanceState);
        ViewGroup parent = (ViewGroup) inflater.inflate(R.layout.fragment_contact_list, container, false);
        parent.addView(v, 0);
        return parent;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if(Build.VERSION.SDK_INT >= 11)
            new LoadCachedContacts().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        else
            new LoadCachedContacts().execute();

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Contact c = mContactAdapter.getItem(position);
        addConversation(c);
    }

    private void addConversation(Contact contact) {
        ((ChatActivity)getActivity()).addConversation(contact);
//        mContactSearchEditText.setText("");

        Conversation newConversation;

        try {
            newConversation = ConversationUtil.findByParticipant(contact, getActivity());
        } catch(NotFoundInDatabaseException e) {
            try {
                newConversation = ConversationUtil.createConversation(contact, getActivity());
            } catch (InvalidConversationException e1) {
                //TODO let user know something went wrong
                return;
            }
        }


        ConversationEvent event = new ConversationEvent(
                ConversationEvent.ConversationEventType.SWITCHED, newConversation);
        EventBus.getDefault().postSticky(event);
    }

    public void filter(String query) {
        mContactAdapter.filter(query);
    }

    private class LoadCachedContacts extends AsyncTask<Void, Void, List<Contact>> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            setListShown(false);
        }

        @Override
        protected List<Contact> doInBackground(Void... params) {
            return ContactUtil.getCachedContacts(getActivity().getApplicationContext());
        }

        @Override
        protected void onPostExecute(List<Contact> contacts) {
            super.onPostExecute(contacts);

            mContactAdapter = new ContactAdapter(getActivity().getApplicationContext(), contacts);
            setListAdapter(mContactAdapter);

            //setup listview
            getListView().setFastScrollEnabled(true);
            getListView().setOnItemClickListener(ContactListFragment.this);
            getListView().setOnScrollListener(new AbsListView.OnScrollListener() {
                int mLastFirstVisibleItem = 0;

                @Override
                public void onScrollStateChanged(AbsListView view, int scrollState) {

                }

                @Override
                public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                    final int currentFirstVisibleItem = getListView().getFirstVisiblePosition();

                        if (currentFirstVisibleItem > mLastFirstVisibleItem) {
                        ((ChatActivity) getActivity()).getSupportActionBar().hide();

//                            Toolbar toolbar = ((ChatActivity) getActivity()).getToolbar();
//                            toolbar.animate().translationY(-toolbar.getHeight()).setInterpolator(new AccelerateInterpolator(2));
                        } else if (currentFirstVisibleItem < mLastFirstVisibleItem) {
                        ((ChatActivity) getActivity()).getSupportActionBar().show();
//                            Toolbar toolbar = ((ChatActivity) getActivity()).getToolbar();
//                            toolbar.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2));
                        }


                    mLastFirstVisibleItem = currentFirstVisibleItem;
                }
            });

            setListShown(true);
        }
    }
}

