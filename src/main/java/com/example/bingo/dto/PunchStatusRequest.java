package com.example.bingo.dto;

import lombok.Data;

/**
 * ビンゴカードのマスの状態更新リクエストを受け取るためのDTO（Data Transfer Object）クラス。
 */
@Data
public class PunchStatusRequest {
    /** 穴をあける（または戻す）マスのインデックス (0〜24) */
    private int index;
    /** 新しい状態 (true: 穴あき, false: 穴なし) */
    private boolean status;
}
