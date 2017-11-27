function mobileNumberValidation(mobile_num)
{
 if(mobile_num.length == mobileNumberLength)
 {
return true;
 }
}

$(document).ready(
    function() {
    var rep="x";
    var placeholdervalue="99";
        $('div.rupee').html(CURRENCY_SYM);
        for (var i = 0; i < mobileNumberLength-2; i++)
        {
       placeholdervalue = placeholdervalue.concat(rep);
        }
         $("#mobile_num").attr("placeholder", placeholdervalue);
    }
);

