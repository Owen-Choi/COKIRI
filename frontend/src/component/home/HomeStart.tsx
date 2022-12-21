import React, {useState, useEffect, useMemo, useCallback} from 'react';
// import styles from "../../styles/home/Home.module.scss";
import styles from "../../styles/home/HomeStart.module.css"
import { useNavigate } from 'react-router-dom';

const HomeStart = () => {
    const navigate = useNavigate();


    const onClickStart = () => {
        navigate(`/login`)
    }
    return (
        <section className={styles.start}>
            <div className={styles.startLeft}>CO끼리</div>
            <div className={styles.startRight}>
                <div className={styles.startRight1}>
                    중고 거래부터 동네 인증까지, 코끼리와 함께해요.<br/>
                    가볍고 따뜻한 코끼리를 만들어요.</div>
                <div className={styles.startRight2}>
                    <button onClick={onClickStart}>시작하기</button>
                    <button>내 물건 올리기</button>
                </div>
            </div>
        </section>
    );
}

export default HomeStart;