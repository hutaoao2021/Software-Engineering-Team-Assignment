//查询所有订单
function orderListApi() {
  return $axios({
    'url': '/user/list',
    'method': 'get',
  })
}

//查询单个用户
// 注意，这里的id就是手机号
function userFindOneApi(id) {
    return $axios({
        'url': `/user/${id}`,
        'method': 'get',
    })
}