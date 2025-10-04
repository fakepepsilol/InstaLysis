package rs.fpl.instalysis.handlers

import android.os.Bundle
import android.util.Log

@Suppress("unused")
inline fun <T> tryOrNull(block: () -> T): T? =
    try { block() } catch (_: Exception) { null }

object MessageHandler{
    const val TAG = "FPL_MessageHandler"
    fun handleMessage(bundle: Bundle){
        val obj = bundle.getString("serializedMessage")!!
        Log.w(TAG, obj)

        if(obj.contains("what=2")){
            return
        }
        //{
        //  when=0
        //  what=1
        //  obj=
        //  [
        //      {
        //          "event":"patch",
        //          "data":
        //              [
        //                  {
        //                      "op":"add",
        //                      "path":"/direct_v2/threads/340282366841710301244276132372040051115/items/32458478705643973444533335143481344",
        //                      "value":
        //                          "{
        //                              \"item_id\":\"32458478705643973444533335143481344\",
        //                              \"message_id\":\"mid.$cAAACgYvYL9Kf4EWUW2ZrwBLH_6_s\",
        //                              \"user_id\":62529557432,
        //                              \"timestamp\":1759577656411684,
        //                              \"item_type\":\"text\",
        //                              \"client_context\":\"7380203601274056684\",
        //                              \"show_forward_attribution\":false,
        //                              \"forward_score\":null,
        //                              \"is_shh_mode\":false,
        //                              \"otid\":\"7380203601274056684\",
        //                              \"is_ae_dual_send\":false,
        //                              \"is_ephemeral_exception\":false,
        //                              \"is_disappearing\":false,
        //                              \"is_superlative\":false,
        //                              \"paid_partnership_info\":{\"is_paid_partnership\":false},
        //                              \"is_replyable_in_bc\":false,
        //                              \"skip_bump_thread\":false,
        //                              \"can_have_attachment\":true,
        //                              \"is_cutout_sticker_creation_allowed\":false,
        //                              \"send_attribution\":\"inbox\",
        //                              \"latest_snooze_state\":0,
        //                              \"one_click_upsell\":null,
        //                              \"genai_params\":{},
        //                              \"text\":\"asdf\",
        //                              \"snippet\":{}
        //                          }"
        //                  }
        //              ],
        //          "message_type":1,
        //          "seq_id":915,
        //          "tq_seq_id":null,
        //          "mi_trace_id":-6720905219186999028,
        //          "mutation_token":"7380203601274056684",
        //          "client_context":"7380203601274056684",
        //          "realtime":true,
        //          "delta_type":"deltaNewMessage"
        //      }
        //  ]
        //  target=X.BvP
        //}





//        val jsonData: String? = Regex(""".+obj=(?<jsonData>\[\{.+\}]).+\}.*""").find(obj)?.groups["jsonData"]?.value

//        crashes sometimes idk why
//        val dataObject = JSONObject(JSONArray(jsonData).getJSONObject(0).getJSONArray("data").getJSONObject(0).getString("value"))
//        Log.i(tag, dataObject.toString())
    }
}
