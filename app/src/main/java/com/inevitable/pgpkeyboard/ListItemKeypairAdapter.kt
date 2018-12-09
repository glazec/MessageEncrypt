package com.inevitable.pgpkeyboard

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.TextView


class ListItemKeypairAdapter(context: Context, list: MutableList<String>) : BaseAdapter() {
    private var mContext = context
    private var mList = list
    override fun getView(position: Int, convertView: View, parent: ViewGroup?): View {
        //To change body of created functions use File | Settings | File Templates.
        var viewHolder: ViewHolder? = null;
        var view = convertView

        if (view == null) {
            viewHolder = ViewHolder()
            view = LayoutInflater.from(mContext).inflate(R.layout.list_item_keypair, null)
            viewHolder.mTextView = view.findViewById(R.id.item_tv) as TextView
            viewHolder.mButton = view.findViewById(R.id.item_btn) as Button
            view.setTag(viewHolder)
        } else {
            viewHolder = view.getTag() as ViewHolder
        }

        viewHolder.mTextView.setText(mList.get(position))
        viewHolder.mButton.setOnClickListener {
            mOnItemDeleteListener.onDeleteClick(position);
        }


        return view
    }

    interface onItemDeleteListener {
        fun onDeleteClick(i: Int)
    }

    private lateinit var mOnItemDeleteListener: onItemDeleteListener

    fun setOnItemDeleteClickListener(mOnItemDeleteListener: onItemDeleteListener) {
        this.mOnItemDeleteListener = mOnItemDeleteListener
    }

    override fun getCount(): Int {
        return mList.size//To change body of created functions use File | Settings | File Templates.
    }

    override fun getItem(position: Int): Any {
        return mList[position] //To change body of created functions use File | Settings | File Templates.
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()//To change body of created functions use File | Settings | File Templates.
    }

    class ViewHolder {
        lateinit var mTextView: TextView
        lateinit var mButton: Button
    }

}


