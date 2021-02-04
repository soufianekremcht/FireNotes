package com.soufianekre.firenotes.ui.base


import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import es.dmoral.toasty.Toasty

abstract class BaseFragment<T : ViewDataBinding,V:BaseViewModel> : Fragment() {

    private var mActivity : BaseActivity<*,*>?= null
    private lateinit var mViewBinding : T
    private var mViewModel : V? = null


    abstract fun getLayoutId() : Int
    abstract fun getViewModel() : V

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mViewBinding = DataBindingUtil.inflate(inflater,getLayoutId(),container,false)
        return mViewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mViewBinding.lifecycleOwner = this
        mViewBinding.executePendingBindings()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        /*
        if (context is BaseActivity<*, *>) {
            val activity = context as BaseActivity<*, *>
            this.mActivity = activity
            activity.onFragmentAttached()
        }

         */
    }

    fun showInfo(msg :String){
        Toasty.info(requireActivity(),msg, Toasty.LENGTH_SHORT).show()

    }

    fun showSuccess(msg :String){
        Toasty.success(requireActivity(),msg, Toasty.LENGTH_SHORT).show()
    }

    fun showError(msg :String){
        Toasty.error(requireActivity(),msg, Toasty.LENGTH_SHORT).show()
    }


    public fun getViewBinding() : T{
        return mViewBinding!!
    }




}
