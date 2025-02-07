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
   implementation 'com.github.Amankhan-mobipixels:MobiAudioPlayer:1.1.0'
}

````
How to use:
val bitmap = BitmapFactory.decodeResource(resources, R.drawable.sample_image)
        val intent = Intent(this, MobiAudioPlayer::class.java)
        intent.putExtra("playListName","Downloads")
        intent.putStringArrayListExtra("data", list)
	intent.putExtra("icon", bitmap)
        intent.putExtra("iconSize",150)
	intent.putExtra("position",1)
        startActivity(intent)
   
