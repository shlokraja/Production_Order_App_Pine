# Foodbox-Outlet
This is the repo for the outlet side of food box.

## This has 3 parts
1. outlet_app -> This is the main node web service
2. menu_display -> This is a daemon which outputs images to be displayed on
monitors
3. MSwipeInterface -> This is the ordering app along with the payment gateway.
The order_app related stuff is done solely using html and js, which runs in a
webview. The payment gateway code is done in android java.

Redis is used to store the stock count data in memory. Also a couple of other things like the dispenser status, expiry time of items etc.
