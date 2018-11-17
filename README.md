MultifunctionAdapter
==========

Provides a simple way to extend the default RecyclerView behaviour with support for headers, footers, empty view, DiffUtil and ArrayAdapter like methods.

## Features

### 1. Header & footer support

Add vs Remove a custom number of headers and footers to [MultifunctionAdapter](https://github.com/VNAPNIC/MultifunctionAdapter/blob/master/app/src/main/java/com/nankai/multifunctionadapter/adapter/MultiFunctionAdapter.kt) by using 3 simple methods from [MultifunctionAdapter](https://github.com/VNAPNIC/MultifunctionAdapter/blob/master/app/src/main/java/com/nankai/multifunctionadapter/adapter/MultiFunctionAdapter.kt):

```java
    //Header
    adapter?.setHeaderView(view: View?)

    adapter?.setHeaderView(view: View?, orientation: Int?)

    adapter?.addHeaderView(view: View?, index: Int?, orientation: Int?)

    adapter?.removeHeaderView(view: View?)

    adapter?.removeAllHeaderView()

    //Footer
    adapter?.setFooterView(view: View?)

    adapter?.setFooterView(view: View?, orientation: Int?)

    adapter?.addFooterView(view: View?, index: Int?, orientation: Int?)

    adapter?.removeFooterView(view: View?)

    adapter?.removeAllFooterView()
```    

### 2. Empty view

Empty view support for [MultifunctionAdapter](https://github.com/VNAPNIC/MultifunctionAdapter/blob/master/app/src/main/java/com/nankai/multifunctionadapter/adapter/MultiFunctionAdapter.kt). View is automatically hidden when adapter is populated with some data, and is automatically shown once again when adapter becomes empty.

```java
     adapter?.setEmptyView(View view)
```  
 
You can also show empty view while adapter is not set to the MultifunctionAdapter, which is handy if you want to intialize adapter at some later point in the time.

```java  
    // show empty view if adapter is not set
    adapter?.setEmptyView(View view, true)
```   
### 3. DiffUtil

DiffUtil support for [MultifunctionAdapter](https://github.com/VNAPNIC/MultifunctionAdapter/blob/master/app/src/main/java/com/nankai/multifunctionadapter/adapter/MultiFunctionAdapter.kt). Simply add [DiffUtil.Callback](https://developer.android.com/reference/android/support/v7/util/DiffUtil.Callback.html) in adapters update method:

```java
    adapter?.update(new ItemDiffUtilResult())
```    

As [DiffUtil](https://developer.android.com/reference/android/support/v7/util/DiffUtil.html) is a blocking sync action, it's executed on the background thread inside the  [MultifunctionAdapter](https://github.com/VNAPNIC/MultifunctionAdapter/blob/master/app/src/main/java/com/nankai/multifunctionadapter/adapter/MultiFunctionAdapter.kt) by using a [AsyncTask](https://developer.android.com/reference/android/os/AsyncTask.html). As a result of this approach, you need to call [cancel()](https://github.com/VNAPNIC/MultifunctionAdapter/blob/master/app/src/main/java/com/nankai/multifunctionadapter/adapter/MultiFunctionAdapter.kt#L126) method on your adapter when your activity or fragment is about to be destroyed, so that the adapter is not updated if the screen has been destroyed.

```java
    override fun onDestroy() {
        super.onDestroy()
        adapter?.cancel()
    }
``` 

### 4. ArrayAdapter like methods

[MultifunctionAdapter](https://github.com/VNAPNIC/MultifunctionAdapter/blob/master/app/src/main/java/com/nankai/multifunctionadapter/adapter/MultiFunctionAdapter.kt) has full support methods: add(), addAll(), reset(), remove(), set()...
