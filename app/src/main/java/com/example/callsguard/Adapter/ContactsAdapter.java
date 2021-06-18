package com.example.callsguard.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.callsguard.Class.Contact;
import com.example.callsguard.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ContactsViewHolder> implements Filterable {
    private ArrayList<Contact> contacts;
    private ArrayList<Contact> contactsAll;
    private OnItemClickListener mlistener;

    Filter filter= new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<Contact> filterdList = new ArrayList();
            if(constraint.toString().isEmpty()){
                filterdList.addAll(contactsAll);
            }else{
                for(Contact contact:contactsAll){
                    if(contact.getName().toLowerCase().contains(constraint.toString().toLowerCase())){
                        filterdList.add(contact);
                    }
                }
            }

            FilterResults filterResults=new FilterResults();
            filterResults.values=filterdList;

            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            contacts.clear();
            contacts.addAll((Collection<? extends Contact>) results.values);
            notifyDataSetChanged();
        }
    };

    @Override
    public Filter getFilter() {
        return filter;
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mlistener = listener;
    }

    public static class ContactsViewHolder extends RecyclerView.ViewHolder {
        public TextView contactName;
        public TextView contactNumber;
        public ImageView isBusy;

        public ContactsViewHolder(@NonNull View itemView, final OnItemClickListener listener) {
            super(itemView);
            contactName = itemView.findViewById(R.id.contactName);
            contactNumber = itemView.findViewById(R.id.contactNumber);
            isBusy = itemView.findViewById(R.id.isBusy);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onItemClick(position);
                        }
                    }
                }
            });

        }
    }

    public ContactsAdapter(ArrayList<Contact> contacts) {
        this.contacts = contacts;
        this.contactsAll=new ArrayList<>(contacts);
    }

    @NonNull
    @Override
    public ContactsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_item, parent, false);
        ContactsViewHolder contactsViewHolder = new ContactsViewHolder(v, mlistener);

        return contactsViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ContactsViewHolder holder, int position) {
        Contact currentContactItem = contacts.get(position);

        holder.contactName.setText(currentContactItem.getName());
        holder.contactNumber.setText(currentContactItem.getNumber());
        if (currentContactItem.isBusy()) {
            Picasso.get().load(R.drawable.busy).into(holder.isBusy);
        } else {
             holder.isBusy.setImageDrawable(null);
        }


    }

    @Override
    public int getItemCount() {
        return contacts.size();
    }
}


