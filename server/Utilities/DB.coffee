monk = require('monk')

db = monk('mongodb://dev:password@ds059682.mongolab.com:59682/waterfall')


module.exports = db
