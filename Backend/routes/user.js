// user.js


const {MongoClient} = require('mongodb');
const uri = 'mongodb://127.0.0.1:27017'; // Replace with your MongoDB connection string
const client = new MongoClient(uri);

// ChatGPT usage: Yes
const createNewUser = async (req, res) => {
  //try {
    // Extract user data from the request body
    const userData = req.body;


    // Assuming you have already connected to the MongoDB client
    const collection = client.db('UserDB').collection('userlist');


    // Check if a user with a specific identifier (e.g., email) already exists
    const existingUser = await collection.findOne({ email: userData.email });


    if (existingUser) {
      // If a user with the same email exists, return an error message
      //await collection.insertOne(userData);
      const errorMessage = 'User with this email already exists';
      const errorMessageLength = Buffer.byteLength(errorMessage, 'utf8');
      res.set('Content-Length', errorMessageLength);
      res.status(409).json({ message: errorMessage });
    } else {
      // If the user doesn't exist, insert the new user document into the collection
      await collection.insertOne(userData);
      const successMessage = 'User created successfully';
      const successMessageLength = Buffer.byteLength(successMessage, 'utf8');
      res.set('Content-Length', successMessageLength);
      res.status(201).json({ message: successMessage });
      //console.log("existing user");
    }
  /*} catch (error) {
    console.error('Error:', error);
    res.status(500).json({ error: 'Internal server error' });
  }*/
};

// ChatGPT usage: Yes
const updateAddress = async (req, res) => {
  //try {
    const email = req.params.email; // Get the email from the URL parameter
    const newAddress = req.body.address; // Get the new address from the request body

    // Assuming you have already connected to the MongoDB client
    const collection = client.db('UserDB').collection('userlist');

    // Update the user's address by their email
    const result = await collection.updateOne(
      { email }, // Use the email to identify the user
      { $set: { address: newAddress } } // Set the new address
    );

    if (result.modifiedCount === 1) {
      res.status(200).json({ message: 'User address updated successfully' });
    } else {
      res.status(404).json({ error: 'User not found' });
    }
  /*} catch (error) {
    console.error('Error:', error);
    res.status(500).json({ error: 'Internal server error' });
  }*/
  };

  // ChatGPT usage: Yes
const getUserByEmail = async (req, res) => {
    //try {
      // Extract the user's email from the request parameters
      const userEmail = req.params.email;
  
      // Assuming you have already connected to the MongoDB client
      const collection = client.db('UserDB').collection('userlist');

      // console.log(userEmail);
  
      // Find the user by their email
      const user = await collection.findOne({ email: userEmail });
  
      if (user) {
        // User found, send user information as a response
        res.status(200).json(user);
        
      } else {
        // User not found
        res.status(404).json({ error: 'User not found' });
      }
    //} catch (error) {
    //  console.error('Error:', error);
    //  res.status(500).json({ error: 'Internal server error' });
    //}
  };

  // ChatGPT usage: Yes
  /*
  const getUserName = async (req, res) => {
    try {
      // Extract the user's email from the request parameters
      const userEmail = req.params.email;
  
      // Assuming you have already connected to the MongoDB client
      const collection = client.db('UserDB').collection('userlist');
  
      // Find the user by their email
      const user = await collection.findOne({ email: userEmail });
  
      if (user) {
        // User found, send the user's name as a response
        res.status(200).json({ name: user.name });
      } else {
        // User not found
        res.status(404).json({ error: 'User not found' });
      }
    } catch (error) {
      console.error('Error:', error);
      res.status(500).json({ error: 'Internal server error' });
    }
  };
  */

  /*
  // ChatGPT usage: Yes
  const getUserAddress = async (req, res) => {
    try {
      // Extract the user's email from the request parameters
      const userEmail = req.params.email;
  
      // Assuming you have already connected to the MongoDB client
      const collection = client.db('UserDB').collection('userlist');
  
      // Find the user by their email
      const user = await collection.findOne({ email: userEmail });
  
      if (user) {
        // User found, send the user's name as a response
        res.status(200).json({ name: user.address });
      } else {
        // User not found
        res.status(404).json({ error: 'User not found' });
      }
    } catch (error) {
      console.error('Error:', error);
      res.status(500).json({ error: 'Internal server error' });
    }
  };
  */

/*
// Function to get a user's friend list from 
// ChatGPT usage: Yes
const getFriendList = async (req, res) => {
    try {
      const userEmail = req.params.email; // User ID whose friend list needs to be retrieved
      //console.log(userId)
      // Assuming you have already connected to the MongoDB client
      const collection = client.db('UserDB').collection('userlist');
  
      // Find the user by their ID (assuming user ID is stored as a string)
      const user = await collection.findOne({ email: userEmail });
  
      if (!user) {
        res.status(404).json({ error: 'User not found' });
      } else {
        const friends = user.friends;
        res.status(200).json(friends);
      }
    } catch (error) {
      console.error('Error:', error);
      res.status(500).json({ error: 'Internal server error' });
    }
  };
  */

  // ChatGPT usage: Yes
const getFriendListWithNames = async (req, res) => {
  //try {
    const userEmail = req.params.email; // User email whose friend list needs to be retrieved

    // Assuming you have already connected to the MongoDB client
    const collection = client.db('UserDB').collection('userlist');

    // Find the user by their email
    const user = await collection.findOne({ email: userEmail });

    var hasFriends = false;

    if (!user) {
      return res.status(404).json({ error: 'User not found' });
    } else {
      const friendEmails = user.friends;
      const friendRequestEmails = user.friendRequests;

      // Create an array to store friend details (email and name)
      const friendsWithNames = [];
      const friendRequestsWithNames = [];

      // Retrieve names for friends
      if (friendEmails.length > 0) {
        hasFriends = true;
      for (const friendEmail of friendEmails) {
        const friend = await collection.findOne({ email: friendEmail });
          
          friendsWithNames.push({
            email: friend.email,
            name: friend.name,
          });
      }
    }

      // Retrieve names for friend requests
      if (friendRequestEmails.length > 0) {
        hasFriends = true;
      for (const friendRequestEmail of friendRequestEmails) {
        const friendRequestUser = await collection.findOne({ email: friendRequestEmail });

          friendRequestsWithNames.push({
            email: friendRequestUser.email,
            name: friendRequestUser.name,
          });
        
      }
    }

      // Include the list of friendRequests and friends in the response
      if (hasFriends) {
        res.status(200).json({ friendsWithNames, friendRequestsWithNames });
      } else {
        res.status(201).json({friendsWithNames, friendRequestsWithNames});
      }
      
    }
  /*} catch (error) {
    console.error('Error:', error);
    res.status(500).json({ error: 'Internal server error' });
  }*/
};

  
/*
// ChatGPT usage: Yes
  const addFriend = async (req, res) => {
    try {
      const userEmail = req.params.email; // User's email for whom the friend list needs to be updated
      const friendEmail = req.body.email; // Friend's email to be added
  
      // Assuming you have already connected to the MongoDB client
      const collection = client.db('UserDB').collection('userlist');
  
      // Find the user by their email
      // Check if the user or friend doesn't exist in the userlist collection
      const [user, friend] = await Promise.all([
        collection.findOne({ email: userEmail }),
        collection.findOne({ email: friendEmail }),
      ]);
  
      if (!user) {
        res.status(404).json({ error: 'User not found' });
        return;
      }

      
      console.log(friend);

      if (!friend) {
        res.status(404).json({ error: 'Friend not found' });
        return;
      }
  
      
      // Check if the friend's email exists in the user's friend list
      const friendExists = user.friends.includes(friendEmail);
  
      if (!friendExists) {
        // Friend not found, add the friend
        user.friends.push(friendEmail);
  
        // Update the document in the collection
        const updateResult = await collection.updateOne(
          { _id: user._id },
          { $set: { friends: user.friends } }
        );
  
        if (updateResult.modifiedCount > 0) {
          res.status(200).json({ message: 'Friend added successfully' });
        } else {
          res.status(500).json({ error: 'Failed to add friend' });
        }
      } else {
        res.status(400).json({ error: 'Friend already exists' });
      }
    } catch (error) {
      console.error('Error:', error);
      res.status(500).json({ error: 'Internal server error' });
    }
  };
  
// ChatGPT usage: Yes
  const deleteFriend = async (req, res) => {
    try {
      const userEmail = req.params.email; // User's email for whom the friend list needs to be updated
      const friendEmail = req.body.email; // Friend's email to be deleted
  
      // Assuming you have already connected to the MongoDB client
      const collection = client.db('UserDB').collection('userlist');
  
      // Find the user by their email
      const user = await collection.findOne({ email: userEmail });
  
      if (!user) {
        res.status(404).json({ error: 'User not found' });
        return;
      }
  
      // Check if the friend's email exists in the user's friend list
      const friendIndex = user.friends.indexOf(friendEmail);
  
      if (friendIndex !== -1) {
        // Friend found, delete the friend
        user.friends.splice(friendIndex, 1);
  
        // Update the document in the collection
        const updateResult = await collection.updateOne(
          { _id: user._id },
          { $set: { friends: user.friends } }
        );
  
        if (updateResult.modifiedCount > 0) {
          res.status(200).json({ message: 'Friend deleted successfully' });
        } else {
          res.status(500).json({ error: 'Failed to delete friend' });
        }
      } else {
        res.status(400).json({ error: 'Friend not found in the list' });
      }
    } catch (error) {
      console.error('Error:', error);
      res.status(500).json({ error: 'Internal server error' });
    }
  };
  */
  // ChatGPT usage: Yes
  const sendFriendRequest = async (req, res) => {
    //try {
      const userEmail = req.params.email; // User's email
      const friendEmail = req.body.email; // Friend's email to be added to friend requests
  
      const userCollection = client.db('UserDB').collection('userlist');
  
      // Check if the user or friend doesn't exist in the userlist collection
      const [user, friend] = await Promise.all([
        userCollection.findOne({ email: userEmail }),
        userCollection.findOne({ email: friendEmail }),
      ]);
  
      
      if (!user) {
        return res.status(221).json({ message: 'User not found' });
      }
      
  
      if (!friend) {
        console.log(`Friend with email ${friendEmail} not found in the userlist.`);
        return res.status(222).json({ message: 'Friend not found in the userlist' });
      }
  
      /*
      // Check if the user has friendRequests property, and if not, initialize it as an empty array
      if (!user.friendRequests) {
        user.friendRequests = [];
      }
      */
  
      // Check if the friend's email is already in the user's friend requests or friends

      if (friend.friendRequests.includes(userEmail)){
        return res.status(250).json({ message: 'Friend request already sent' });
      }
        else if (user.friendRequests.includes(friendEmail)){
        return res.status(251).json({ message: 'You have already received a friend request from this person' });
      }
      else if (user.friends.includes(friendEmail)){
        return res.status(252).json({ message: 'Already friends with this user' });
      }
      else {

        //const friendFound = userCollection.findOne({ email: friendEmail });


        // Friend not found in friend requests, add the friend's email
        //user.friendRequests.push(friendEmail);
        friend.friendRequests.push(userEmail);
  
        // Update the user's document in the collection
        await userCollection.updateOne(
          { _id: friend._id },
          { $set: { friendRequests: friend.friendRequests } }
        );
  
        //if (updateResult.modifiedCount > 0) {
          return res.status(200).json({ message: 'Friend request sent successfully' });
        //} else {
        //  return res.status(500).json({ error: 'Failed to send friend request' });
        //}
      } 
    /*} catch (error) {
      console.error('Error:', error);
      return res.status(500).json({ error: 'Internal server error' });
    }*/
  };
  // ChatGPT usage: Yes
  const acceptFriendRequest = async (req, res) => {
    //try {
      const userEmail = req.params.email; // User's email
      const friendEmail = req.params.friendRequest; // Friend's email to accept
  
      const userCollection = client.db('UserDB').collection('userlist');
  
      // Check if the user exists in the userlist collection
      const user = await userCollection.findOne({ email: userEmail });
  
      if (!user) {
        return res.status(404).json({ error: 'User not found' });
      }
  
      // Check if the friend's email exists in the user's friendRequests
      if (!user.friendRequests.includes(friendEmail)) {
        return res.status(400).json({ error: 'Friend request not found in the user\'s friend requests' });
      }
  
      // Remove the friend request
      const updatedFriendRequests = user.friendRequests.filter(request => request !== friendEmail);
  
      // Add the friend to the user's list of friends
      user.friends.push(friendEmail);
  
      // Find the friend and add the user to their list of friends
      const friend = await userCollection.findOne({ email: friendEmail });
  
      /*
      if (!friend) {
        return res.status(404).json({ error: 'Friend not found in the userlist' });
      }
      */
  
      friend.friends.push(userEmail);
  
      // Update both user and friend documents
      const updateResults = await Promise.all([
        userCollection.updateOne({ _id: user._id }, { $set: { friendRequests: updatedFriendRequests, friends: user.friends } }),
        userCollection.updateOne({ _id: friend._id }, { $set: { friends: friend.friends } }),
      ]);
      updateResults.every(result => result.modifiedCount);
      return res.status(200).json({ message: 'Friend request accepted successfully' });
  
      /*
      if (updateResults.every(result => result.modifiedCount > 0)) {
        return res.status(200).json({ message: 'Friend request accepted successfully' });
      } else {
        return res.status(500).json({ error: 'Failed to accept friend request' });
      }
      */
    /*} catch (error) {
      console.error('Error:', error);
      return res.status(500).json({ error: 'Internal server error' });
    }*/
  };
// ChatGPT usage: Yes
  const declineFriendRequest = async (req, res) => {
    //try {
      const userEmail = req.params.email; // User's email
      const friendEmail = req.params.friendRequest; // Friend's email to decline
  
      const userCollection = client.db('UserDB').collection('userlist');
  
      // Check if the user exists in the userlist collection
      const user = await userCollection.findOne({ email: userEmail });
  
      if (!user) {
        return res.status(404).json({ error: 'User not found' });
      }
  
      // Check if the friend's email exists in the user's friendRequests
      if (!user.friendRequests.includes(friendEmail)) {
        return res.status(400).json({ error: 'Friend request not found in the user\'s friend requests' });
      }
  
      // Remove the friend request
      const updatedFriendRequests = user.friendRequests.filter(request => request !== friendEmail);
  
      // Update the user's document in the collection to remove the friend request
      await userCollection.updateOne(
        { _id: user._id },
        { $set: { friendRequests: updatedFriendRequests } }
      );
  
      //if (updateResult.modifiedCount > 0) {
        return res.status(200).json({ message: 'Friend request declined successfully' });
      //} else {
      //  return res.status(500).json({ error: 'Failed to decline friend request' });
      //}
    /*} catch (error) {
      console.error('Error:', error);
      return res.status(500).json({ error: 'Internal server error' });
    }*/
  };
  
  
  


  
  
  
  
  


/*
  // Function to update a user's list of friends in MongoDB
const updateFriendList = async (req, res) => {
    try {
      const userEmail = req.params.email; // User ID whose friend list needs to be updated
      const newFriend = req.body; // Data for the new friend to be added

      // Assuming you have already connected to the MongoDB client
      const collection = client.db('UserDB').collection('userlist');
  
      // Find the user by their ID (assuming user ID is stored as a string)
      const user = await collection.findOne({ email: userEmail });
  
      if (!user) {
        res.status(404).json({ error: 'User not found' });
      } else {
        // Update the user's friend list in MongoDB
        user.friends.push(newFriend);
  
        // Update the document in the collection
        const updateResult = await collection.updateOne(
          { _id: userId },
          { $set: { friends: user.friends } }
        );
  
        if (updateResult.modifiedCount > 0) {
          res.status(200).json({ message: 'Friend list updated successfully' });
        } else {
          res.status(500).json({ error: 'Failed to update friend list' });
        }
      }
    } catch (error) {
      console.error('Error:', error);
      res.status(500).json({ error: 'Internal server error' });
    }
  };
*/

  // Function to update a user's information in MongoDB
  // ChatGPT usage: Yes
  /*
const updateUser = async (req, res) => {
    try {
      const userEmail = req.params.email; // User email whose information needs to be updated
      const updatedUserData = req.body; // Updated user data
  
      // Assuming you have already connected to the MongoDB client
      const collection = client.db('UserDB').collection('userlist');
  
      // Update the user's information in MongoDB
      const updateResult = await collection.updateOne(
        { email: userEmail },
        { $set: updatedUserData }
      );
  
      if (updateResult.modifiedCount > 0) {
        res.status(200).json({ message: 'User information updated successfully' });
      } else {
        res.status(404).json({ error: 'User not found' });
      }
    } catch (error) {
      console.error('Error:', error);
      res.status(500).json({ error: 'Internal server error' });
    }
  };
  */

/*
// Sample user data
const sampleUser = {
    name: 'John Doe',
    schedule_id: '65382a5cfd224fcdb1cdbe60',
    friends: [
      {
        friend_id: '65382a5cfd224fcdb1cdbe5c',
        friend_name: 'Alice Smith',
        schedule_id: '65382a5cfd224fcdb1cdbe5d',
      },
      {
        friend_id: '65382a5cfd224fcdb1cdbe5e',
        friend_name: 'Bob Johnson',
        schedule_id: '65382a5cfd224fcdb1cdbe5f',
      },
    ],
  };
  
  // Function to get a user's friend list
  const getFriendList = async (req, res) => {
    try {
      const userId = req.params.id; // Replace with your user ID parameter
      // In a real application, you would retrieve the user's friend list here
      // For this example, we'll return the sample user's friend list
      const user = sampleUser;
      const friends = user.friends;
  
      res.status(200).json(friends);
    } catch (error) {
      console.error('Error:', error);
      res.status(500).json({ error: 'Internal server error' });
    }
  };

  // Assuming you have imported necessary modules and set up your Express app

// Function to update a user's list of friends
const updateFriendList = async (req, res) => {
    try {
      const userId = req.params.id; // User ID whose friend list needs to be updated
      const newFriend = req.body; // Data for the new friend to be added
  
      // In a real application, you would update the user's friend list in your database.
      // Here, we'll just add the new friend to the sample user's friend list.
  
      // First, retrieve the user's existing friend list (in a real app, you would fetch this from your database).
      const user = sampleUser;
  
      // Update the user's friend list by adding the new friend
      user.friends.push(newFriend);
  
      // In a real application, you would save the updated friend list to your database.
  
      res.status(200).json({ message: 'Friend list updated successfully', user: user });
    } catch (error) {
      console.error('Error:', error);
      res.status(500).json({ error: 'Internal server error' });
    }
  };
*/
// ChatGPT usage: Yes
/*
const deleteUser = async (req, res) => {
  try {
    const email = req.params.email; // Get the email from the URL parameter

    // Delete the user based on their email
    const collection = client.db('UserDB').collection('userlist');
    const result = await collection.deleteOne({ email });

  if (result.deletedCount === 1) {
      res.status(200).json({ message: 'User deleted successfully' });
    } else {
      res.status(404).json({ error: 'User not found' });
    }
  } catch (error) {
    console.error('Error:', error);
    res.status(500).json({ error: 'Internal server error' });
  }
};
*/




  
  
  module.exports = {
    createNewUser,
    updateAddress,
    getUserByEmail,
    //getUserName,
    //getUserAddress,
    //getFriendList,
    getFriendListWithNames,
    //addFriend,
    //deleteFriend,
    //updateUser,
    //deleteUser,
    sendFriendRequest,
    acceptFriendRequest,
    declineFriendRequest,
  };
  
