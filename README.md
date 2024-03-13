# Mobi Audio Player
add maven in your project level gradle
````
allprojects {
	repositories {
		...
		maven { url 'https://jitpack.io' 
		}
	}
}
````
add dependency in module level gradle
````
dependencies:
{
   implementation 'com.github.Amankhan-mobipixels:MobiAudioPlayer:1.0.4'
}

````
How to use:

        val intent = Intent(this, MobiAudioPlayer::class.java)
        intent.putExtra("playListName","Downloads")
        intent.putStringArrayListExtra("data", list)
        startActivity(intent)
   
