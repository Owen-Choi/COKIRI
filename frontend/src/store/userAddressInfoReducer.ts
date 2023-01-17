/**
 * 초기 값의 타입을 지정해주고,
 * action에 PayloadAction 제네릭 타입 추가를 해준다.
 *
 */

import {createSlice,PayloadAction} from "@reduxjs/toolkit";
interface UserAddressInfo {
    addressId1: number,
    addressName1:string,
    addressId2:number
    addressName2:string,
}

const initialUserAddressInfoState : UserAddressInfo = {
    addressId1:0,
    addressName1:'',
    addressId2:0,
    addressName2:'',


}

//state는 이 상태의 현재 상태 값을 의미한다
//action은 실제 페이지 컴포넌트에서 전달해준 값을 의미한다

const userAddressInfoSlice = createSlice({
    name: "userAddressInfo",
    initialState : initialUserAddressInfoState,
    reducers : {
        setUserAddressInfo1(state,action){
            state.addressId1 = action.payload;
        },
        setAddressName1(state,action){
            state.addressName1= action.payload;
        },
        resetaddress1(state){
            state.addressId1 = undefined
            state.addressName1 = ''
        },

        setUserAddressInfo2(state,action){
            state.addressId2 = action.payload;
        },
        setAddressName2(state,action){
            state.addressName2= action.payload;
        },
        resetaddress2(state){
            state.addressId2 = undefined
            state.addressName2 = ''
        }
    }
})


//위에서 선언해준 counterSlice의 reducer를 export해준다
export default userAddressInfoSlice.reducer;
export const {
    setUserAddressInfo1,setAddressName1,resetaddress1,
    setUserAddressInfo2,setAddressName2,resetaddress2
} = userAddressInfoSlice.actions;

//이제 이걸다른 컴포턴트에서 dispatch로 사용한다.

//여기선 알아서 api가 해주는구나, 대충 여기서 선언한 initial state, type, action을 보내주면 된다고 생각하면 되겠네