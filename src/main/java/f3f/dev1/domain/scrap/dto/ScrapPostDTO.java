package f3f.dev1.domain.scrap.dto;

import com.querydsl.core.annotations.QueryProjection;
import f3f.dev1.domain.model.TradeStatus;
import f3f.dev1.domain.trade.model.Trade;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class ScrapPostDTO {


    @Data
    @Builder
    @NoArgsConstructor
    public static class GetUserScrapPost{
        private Long postId;
        private String thumbNail;
        private String title;
        private TradeStatus tradeStatus;
        private String wishCategory;
        private Long scrapCount;

        public GetUserScrapPost(Long postId, String title, String thumbNail, TradeStatus tradeStatus, String wishCategory, Long likeCount) {
            this.postId = postId;
            this.title = title;
            this.thumbNail = thumbNail;
            this.tradeStatus = tradeStatus;
            this.wishCategory = wishCategory;
            this.scrapCount = likeCount;
        }
    }
}
