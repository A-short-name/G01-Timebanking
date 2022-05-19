package it.polito.mad.g01_timebanking

import it.polito.mad.g01_timebanking.adapters.AdvertisementAdapter
import it.polito.mad.g01_timebanking.adapters.AdvertisementDetails
import android.widget.Filter

//class FilterAdv(filterList: List<AdvertisementDetails>, private val adapterAdv: AdvertisementAdapter) : Filter() {
//
//    private val filterList: List<AdvertisementDetails>
//
//    override fun performFiltering(constraint: CharSequence?): FilterResults {
//        var constraint1: CharSequence?=constraint
//        val result = FilterResults()
//        //check constraint validity
//        if(constraint1 != null && constraint1.isNotEmpty()){
//            // case insensitive search
//            constraint1 = constraint1.toString().toUpperCase()
//            // store filtered data
//            val filteredAdvList = List<AdvertisementDetails>
//            for (i in filterList.indices){
//                // check if any item matches with the constraint
//                if(filterList[i].location.toUpperCase().contains(constraint1))
//            }
//            result.count = filteredAdvList.size
//            result.values = filteredAdvList
//        }else{
//            result.count = filterList.size
//            result.values = filterList
//        }
//        return result
//    }
//
//    override fun publishResults(p0: CharSequence?, result: FilterResults) {
//        adapterAdv.data = result.values as List<AdvertisementDetails>
//        adapterAdv.notifyDataSetChanged()
//    }
//
//}