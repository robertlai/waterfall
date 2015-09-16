express = require('express')
api = require('./api')
router = express.Router()


router.use(api)

router.get '/', (req, res, next) ->
    res.render 'index', title: 'Waterfall API'


module.exports = router
