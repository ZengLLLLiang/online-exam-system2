<template>
  <el-container>
    <el-header>
      <!--操作的下拉框-->
      <el-select @change="operation" clearable v-model="queryInfo.examId"
                 placeholder="请选择考试" style="margin-bottom: 25px;">
        <el-option v-for="(item,index) in allExamInfo" :key="index" :value="parseInt(item.examId)">
          <span style="float: left">{{ item.examName }}</span>
        </el-option>
      </el-select>

    </el-header>

    <el-main>
      <el-table
        ref="questionTable"
        highlight-current-row
        v-loading="loading"
        :border="true"
        :data="examRecords"
        tooltip-effect="dark"
        style="width: 100%;">

        <el-table-column align="center" prop="examName" label="考试名称"></el-table-column>

        <el-table-column align="center" prop="examTime" label="考试时间"></el-table-column>

        <el-table-column align="center" prop="trueName" label="考生"></el-table-column>

        <el-table-column align="center" prop="logicScore" label="逻辑题得分"></el-table-column>

        <el-table-column align="center" label="是否批阅">
          <template slot-scope="scope">
            <span>{{ scope.row.totalScore === null ? '未批阅' : '已批阅' }}</span>
          </template>
        </el-table-column>

        <el-table-column align="center" label="总分">
          <template slot-scope="scope">
            <span>{{ scope.row.totalScore === null ? 0 : scope.row.totalScore }}</span>
          </template>
        </el-table-column>

        <el-table-column align="center" label="操作">
          <template slot-scope="scope">
            <el-button type="info" icon="el-icon-camera" size="small" style="margin-right: 5px;"
                       @click="viewCameraImages(scope.row.recordId)">
              查看摄像头照片
            </el-button>
            <el-button :type="scope.row.totalScore === null ? 'primary' : 'warning'" icon="el-icon-view" size="small" :disabled="scope.row.totalScore !== null"
                       @click="$router.push('/markExam/' + scope.row.recordId)">
              {{ scope.row.totalScore === null ? '批阅' : '已批阅'}}
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!--分页-->
      <el-pagination style="margin-top: 25px"
                     @size-change="handleSizeChange"
                     @current-change="handleCurrentChange"
                     :current-page="queryInfo.pageNo"
                     :page-sizes="[10, 20, 30, 50]"
                     :page-size="queryInfo.pageSize"
                     layout="total, sizes, prev, pager, next, jumper"
                     :total="total">
      </el-pagination>
    </el-main>

    <!--摄像头照片对话框-->
    <el-dialog :visible.sync="cameraDialog" title="摄像头照片" @close="cameraDialog = false" width="80%">
      <div v-if="cameraImages.length === 0" style="text-align: center; padding: 40px;">
        <p>该考试记录暂无摄像头照片</p>
      </div>
      <div v-else style="display: flex; flex-wrap: wrap; gap: 20px;">
        <div v-for="(imageUrl, index) in cameraImages" :key="index" style="flex: 1; min-width: 300px;">
          <img :src="imageUrl" alt="摄像头照片" style="width: 100%; height: auto; border: 1px solid #ddd; border-radius: 4px; cursor: pointer;" @click="showBigImg(imageUrl)">
          <p style="text-align: center; margin-top: 10px;">照片 {{ index + 1 }}</p>
        </div>
      </div>
    </el-dialog>

    <!--大图查看对话框-->
    <el-dialog :visible.sync="bigImgDialog" @close="bigImgDialog = false" width="90%">
      <img style="width: 100%" :src="bigImgUrl">
    </el-dialog>
  </el-container>
</template>

<script>
  export default {
    name: 'MarkManage',
    data () {
      return {
        queryInfo: {
          pageNo: 1,
          pageSize: 10
        },
        //考试记录
        examRecords: [],
        //表格数据加载
        loading: true,
        //所有考试信息
        allExamInfo: [],
        //总数
        total: 0,
        //摄像头照片对话框
        cameraDialog: false,
        //摄像头照片URL列表
        cameraImages: [],
        //大图对话框
        bigImgDialog: false,
        //大图URL
        bigImgUrl: '',
        //当前用户角色
        currentUserRole: null
      }
    },
    created () {
      this.initData()
    },
    methods: {
      //初始化数据
      async initData () {
        await this.getCurrentUserRole()
        this.getExamRecords()
      },
      //获取当前用户角色
      getCurrentUserRole () {
        return this.$http.get(this.API.checkToken).then((resp) => {
          if (resp.data.code === 200) {
            this.currentUserRole = resp.data.data.roleId
          }
        })
      },
      async getExamRecords () {
        // 根据角色选择API，如果角色还未获取，默认使用老师API
        const apiUrl = this.currentUserRole === '3' ? this.API.getAdminExamRecord : this.API.getExamRecord
        await this.$http.get(apiUrl, { params: this.queryInfo }).then((resp) => {
          if (resp.data.code === 200) {
            this.getExamTotal()
            resp.data.data.forEach(item => {
              this.$http.get(this.API.getUserById + '/' + item.userId).then((r) => {
                item.trueName = r.data.data.trueName
              })
            })
            this.examRecords = resp.data.data
            this.getAllExamInfo()
            this.loading = false
          }
        })
      },
      //查询考试信息
      getExamTotal () {
        let data = JSON.parse(JSON.stringify(this.queryInfo))
        data.pageNo = 1
        data.pageSize = 9999
        // 根据角色选择API
        const apiUrl = this.currentUserRole === '3' ? this.API.getAdminExamRecord : this.API.getExamRecord
        this.$http.get(apiUrl, { params: data }).then((resp) => {
          if (resp.data.code === 200) {
            this.total = resp.data.data.length
          }
        })
      },
      getAllExamInfo () {
        this.$http.get(this.API.allExamInfo).then((resp) => {
          if (resp.data.code === 200) {
            this.allExamInfo = resp.data.data
            this.setExamName()
          }
        })
      },
      operation (v) {
        if (v === '') this.queryInfo.examId = null
        this.getExamRecords()
      },
      setExamName () {
        this.examRecords.forEach(item => {
          this.allExamInfo.forEach(i2 => {
            if (item.examId === i2.examId) {
              this.$set(item, 'examName', i2.examName)
            }
          })
        })
      },
      //分页页面大小改变
      handleSizeChange (val) {
        this.queryInfo.pageSize = val
        this.getExamRecords()
      },
      //分页插件的页数
      handleCurrentChange (val) {
        this.queryInfo.pageNo = val
        this.getExamRecords()
      },
      //查看摄像头照片
      viewCameraImages (recordId) {
        if (this.currentUserRole === '3') {
          // 管理员使用专门的摄像头照片接口
          this.$http.get(this.API.getAdminCameraImages + '/' + recordId).then((resp) => {
            if (resp.data.code === 200) {
              this.cameraImages = resp.data.data || []
              this.cameraDialog = true
            } else {
              this.$message.error('获取摄像头照片失败：' + resp.data.message)
            }
          }).catch(() => {
            this.$message.error('获取摄像头照片失败')
          })
        } else {
          // 老师使用考试记录接口，然后提取摄像头照片
          this.$http.get(this.API.getExamRecordById + '/' + recordId).then((resp) => {
            if (resp.data.code === 200 && resp.data.data.creditImgUrl) {
              this.cameraImages = resp.data.data.creditImgUrl.split(',').filter(url => url.trim() !== '')
              this.cameraDialog = true
            } else {
              this.$message.error('该考试记录暂无摄像头照片')
            }
          }).catch(() => {
            this.$message.error('获取摄像头照片失败')
          })
        }
      },
      //显示大图
      showBigImg (url) {
        this.bigImgUrl = url
        this.bigImgDialog = true
      }
    }
  }
</script>

<style scoped lang="scss">
  .el-container {
    width: 100%;
    height: 100%;
  }

  .el-input {
    width: 200px;
  }

  .el-container {
    animation: leftMoveIn .7s ease-in;
  }

  @keyframes leftMoveIn {
    0% {
      transform: translateX(-100%);
      opacity: 0;
    }
    100% {
      transform: translateX(0%);
      opacity: 1;
    }
  }

  .role {
    color: #606266;
  }

  /deep/ .el-table thead {
    color: rgb(85, 85, 85) !important;
  }

  /*表格的头部样式*/
  /deep/ .has-gutter tr th {
    background: rgb(242, 243, 244);
    color: rgb(85, 85, 85);
    font-weight: bold;
    line-height: 32px;
  }

  .el-table {
    box-shadow: 0 0 1px 1px gainsboro;
  }
</style>
