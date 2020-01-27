package qupath.opencv.ml.objects;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Function;

import qupath.lib.images.ImageData;
import qupath.lib.objects.PathObject;
import qupath.lib.objects.PathObjectFilter;
import qupath.lib.objects.classes.PathClass;
import qupath.lib.objects.classes.PathClassFactory;

class SimpleClassifier extends AbstractObjectClassifier {
	
	private Function<PathObject, PathClass> function;
	private Collection<PathClass> pathClasses;
	
	SimpleClassifier(PathObjectFilter filter, Function<PathObject, PathClass> function, Collection<PathClass> pathClasses) {
		super(filter);
		this.function = function;
		this.pathClasses = Collections.unmodifiableList(new ArrayList<>(pathClasses));
	}

	@Override
	public Collection<PathClass> getPathClasses() {
		return pathClasses;
	}

	@Override
	public int classifyObjects(ImageData<BufferedImage> imageData, Collection<? extends PathObject> pathObjects) {
		int n = 0;
		for (var pathObject : pathObjects) {
			var pathClass = function.apply(pathObject);
			if (pathClass == null)
				continue;
			var currentClass = pathObject.getPathClass();
			if (currentClass == null)
				pathObject.setPathClass(pathClass);
			else
				pathObject.setPathClass(
						PathClassFactory.getDerivedPathClass(currentClass, pathClass.getName(), null)
						);
			n++;
		}
		return n;
	}

}
