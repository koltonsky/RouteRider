const { MongoClient } = require('mongodb');

async function checkMongoDBConnection() {
  const uri = process.env.MONGODB_URI;

  try {
    const client = new MongoClient(uri, { useNewUrlParser: true, useUnifiedTopology: true });
    await client.connect();
    console.log('Connected to MongoDB successfully');
  } catch (error) {
    console.error('Error connecting to MongoDB:', error);
    process.exit(1);
  }
}

checkMongoDBConnection();
